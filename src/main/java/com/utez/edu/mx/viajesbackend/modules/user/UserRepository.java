package com.utez.edu.mx.viajesbackend.modules.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAll();

    User findById(long idUser);

    User save(User user);

    // Eliminar usuario
    @Modifying
    @Query(value = "DELETE FROM user WHERE id = :idUser", nativeQuery = true)
    void deleteById(@Param("idUser") long idUser);

    User findByEmail(String email);

    boolean existsByEmail(String email);


    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByEmailAndIdNot(String email, long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, long id);

    // Choferes pendientes: rol id=3 y status=false
    @Query("select u from User u where u.rol.id = 3 and u.status = false")
    List<User> findPendingDrivers();

}
