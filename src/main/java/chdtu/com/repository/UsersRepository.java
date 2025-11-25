package chdtu.com.repository;

import chdtu.com.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByChatId(Long chatId);

    Optional<Users> getFirstByChatId(Long chatId);

    List<Users> getAllByFaculty(String faculty);

    @Query("select distinct u from Users u left join fetch u.feedbackUsersDB")
    List<Users> findAllWithFeedbacks();

    Optional<Users> getFirstById(Long userId);
}
