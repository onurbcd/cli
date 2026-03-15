package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.persistency.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Modifying
    @Query("delete from Document d where d.id = :id")
    int deleteUsingId(@Param("id") UUID id);
}
