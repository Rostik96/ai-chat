package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.jspecify.annotations.NullMarked;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Character.UnicodeScript.CYRILLIC;
import static java.util.Comparator.comparingDouble;
import static org.springframework.util.StringUtils.hasText;

@NullMarked
@RequiredArgsConstructor
public class Bm25RerankDocumentPostProcessor implements DocumentPostProcessor {

    private static final String CONTENT_FIELD = "content";

    private final int topK;
    private final AnalyzerSelector analyzerSelector = new AnalyzerSelector();
    private final QueryTermExtractor queryTermExtractor = new QueryTermExtractor();
    private final LuceneQueryBuilder luceneQueryBuilder = new LuceneQueryBuilder();
    private final Bm25DocumentScorer bm25DocumentScorer = new Bm25DocumentScorer();


    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (documents.isEmpty()) return documents;

        var limit = Math.max(topK, 1);
        try (var analyzer = analyzerSelector.selectFor(query.text())) {
            var queryTerms = queryTermExtractor.extractDistinct(query.text(), analyzer);
            if (queryTerms.isEmpty()) return documents.stream().limit(limit).toList();

            var luceneQuery = luceneQueryBuilder.build(queryTerms);
            return documents.stream()
                    .map(document -> new RankedDocument(document, bm25DocumentScorer.score(document, analyzer, luceneQuery)))
                    .sorted(comparingDouble(RankedDocument::score).reversed())
                    .limit(limit)
                    .map(RankedDocument::document)
                    .toList();
        }
    }

    private record RankedDocument(Document document, float score) {}

    private static final class AnalyzerSelector {

        Analyzer selectFor(String text) {
            if (!hasText(text)) return new EnglishAnalyzer();
            return text.codePoints().anyMatch(c -> Character.UnicodeScript.of(c) == CYRILLIC)
                    ? new RussianAnalyzer()
                    : new EnglishAnalyzer();
        }
    }

    private static final class QueryTermExtractor {

        List<String> extractDistinct(String text, Analyzer analyzer) {
            if (!hasText(text)) return List.of();

            Set<String> tokens = new LinkedHashSet<>();
            try (TokenStream tokenStream = analyzer.tokenStream(CONTENT_FIELD, text)) {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    tokens.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
                }
                tokenStream.end();
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to tokenize text", e);
            }
            return List.copyOf(tokens);
        }
    }

    private static final class LuceneQueryBuilder {

        org.apache.lucene.search.Query build(List<String> queryTerms) {
            var queryBuilder = new BooleanQuery.Builder();
            for (var term : queryTerms)
                queryBuilder.add(new TermQuery(new Term(CONTENT_FIELD, term)), BooleanClause.Occur.SHOULD);
            return queryBuilder.build();
        }
    }

    private static final class Bm25DocumentScorer {
        private static final float DEFAULT_K1 = 1.2f;
        private static final float DEFAULT_B = 0.75f;
        private static final BM25Similarity BM25 = new BM25Similarity(DEFAULT_K1, DEFAULT_B);

        float score(Document document, Analyzer analyzer, org.apache.lucene.search.Query luceneQuery) {
            if (!hasText(document.getText())) return 0f;

            var memoryIndex = new MemoryIndex();
            memoryIndex.addField(CONTENT_FIELD, document.getText(), analyzer);
            var searcher = memoryIndex.createSearcher();
            searcher.setSimilarity(BM25);
            try {
                var topDocs = searcher.search(luceneQuery, 1);
                if (topDocs.scoreDocs.length == 0) return 0f;
                return topDocs.scoreDocs[0].score;
            }
            catch (IOException e) {
                throw new UncheckedIOException("BM25 scoring failed", e);
            }
        }
    }
}
