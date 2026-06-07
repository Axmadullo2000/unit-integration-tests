package interview.prep.unittests.repository;

import interview.prep.unittests.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User update(User user);
    void deleteById(Long id);
    boolean existsByEmail(String email);
}
