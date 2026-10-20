package ru.practicum.shareit;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ItemQueryIntegrationTest {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private UserRepository users;

    @Autowired
    private ItemRepository items;

    @Autowired
    private BookingRepository bookings;

    @Autowired
    private CommentRepository comments;

    @Autowired
    private ItemService itemService;

    @Test
    void ownerItemsLoadBookingsAndCommentsInFourQueries() {
        User owner = users.save(user("Query owner", "query.owner@example.com"));
        User booker = users.save(user("Query booker", "query.booker@example.com"));
        Item first = items.save(new Item(null, "Drill", "First tool", true, owner, null));
        Item second = items.save(new Item(null, "Saw", "Second tool", true, owner, null));
        LocalDateTime now = LocalDateTime.now();
        Booking past = bookings.save(booking(first, booker, now.minusDays(2), now.minusDays(1)));
        Booking future = bookings.save(booking(second, booker, now.plusDays(2), now.plusDays(3)));
        comments.save(comment(first, booker, "Drill worked", now.minusHours(1)));
        comments.save(comment(second, booker, "Saw worked", now.minusMinutes(1)));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            List<OwnerItemDto> result = itemService.getOwnerItems(owner.getId());

            assertEquals(2, result.size());
            assertEquals(past.getId(), result.get(0).getLastBooking().getId());
            assertEquals(future.getId(), result.get(1).getNextBooking().getId());
            assertEquals("Drill worked", result.get(0).getComments().getFirst().getText());
            assertEquals("Saw worked", result.get(1).getComments().getFirst().getText());
            assertEquals(4, statistics.getPrepareStatementCount());
        } finally {
            statistics.setStatisticsEnabled(false);
        }
    }

    private User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Booking booking(Item item, User booker, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private Comment comment(Item item, User author, String text, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText(text);
        comment.setCreated(created);
        return comment;
    }
}
