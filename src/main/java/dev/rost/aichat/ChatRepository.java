package dev.rost.aichat;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@NullMarked
interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    List<ChatEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "entries")
    Optional<ChatEntity> findById(Long id);
}
