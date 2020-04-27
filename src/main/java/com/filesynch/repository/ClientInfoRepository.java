package com.filesynch.repository;

import com.filesynch.dto.ClientStatus;
import com.filesynch.entity.ClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientInfoRepository extends JpaRepository<ClientInfo, Long> {
    ClientInfo findByLogin(String login);

    @Modifying
    @Query("UPDATE ClientInfo c SET c.status = :status WHERE c.login = :login")
    ClientInfo updateStatus(@Param("login") String login, @Param("status") ClientStatus status);

    @Modifying
    @Query("UPDATE ClientInfo c SET c.login = :login, c.status = :status WHERE c.id = :id")
    ClientInfo updateNew(@Param("id") Long id,
                   @Param("login") String login,
                   @Param("status") ClientStatus status);
}
