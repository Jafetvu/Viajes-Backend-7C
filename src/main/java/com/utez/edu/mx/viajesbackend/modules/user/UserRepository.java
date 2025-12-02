package com.utez.edu.mx.viajesbackend.modules.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(long idUser);

    // Delete user
    @Modifying
    @Query(value = "DELETE FROM user WHERE id = :idUser", nativeQuery = true)
    void deleteById(@Param("idUser") long idUser);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByEmailAndIdNot(String email, long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, long id);

    // Pending drivers: role id=3 and status=false
    @Query("select u from User u where u.role.id = 3 and u.status = false")
    List<User> findPendingDrivers();

    // Count users by role
    long countByRoleId(int roleId);

    // Count users by role and status
    long countByRoleIdAndStatus(int roleId, boolean status);

    // Paginated search with filters
    @Query("SELECT u FROM User u WHERE " +
           "(:roleId IS NULL OR u.role.id = :roleId) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.surname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllWithFilters(
        @Param("roleId") Integer roleId,
        @Param("status") Boolean status,
        @Param("search") String search,
        Pageable pageable
    );
}
