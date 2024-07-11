package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        for (User us : users.values()) {
            if (user.getEmail().equals(us.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            for (User us : users.values()) {
                if (newUser.getEmail() == us.getEmail()) {

                    throw new DuplicatedDataException("Этот имейл уже используется");

                }
            }
            if (newUser.getEmail() == null || newUser.getUsername() == null || newUser.getPassword() == null) {
                return oldUser;
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            oldUser.setUsername(newUser.getUsername());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setPassword(newUser.getPassword());
            oldUser.setRegistrationDate(Instant.now());
            return oldUser;
        }
        throw new NotFoundException("Пост с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}


