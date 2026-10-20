package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static Comment toModel(CreateCommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getAuthor().getName(),
                comment.getCreated());
    }
}
