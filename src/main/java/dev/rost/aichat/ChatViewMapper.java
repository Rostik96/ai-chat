package dev.rost.aichat;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface ChatViewMapper {

    @Mapping(target = "entries", expression = "java(toEntries(chat))")
    ChatView from(ChatEntity chat);

    ChatView.Entry from(ChatEntryEntity entry);

    default List<ChatView.Entry> toEntries(ChatEntity chat) {
        return chat.getEntries().stream().map(this::from).toList();
    }
}
