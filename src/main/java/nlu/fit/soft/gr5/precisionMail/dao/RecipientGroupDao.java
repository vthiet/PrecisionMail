package nlu.fit.soft.gr5.precisionMail.dao;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import nlu.fit.soft.gr5.precisionMail.model.RecipientGroup;

/**
 * DAO Interface for RecipientGroup operations
 */
public interface RecipientGroupDao {
    /**
     * Save new recipient group
     */
    RecipientGroup save(RecipientGroup group) throws IOException;

    /**
     * Update existing recipient group
     */
    RecipientGroup update(RecipientGroup group) throws IOException;

    /**
     * Find recipient group by ID
     */
    Optional<RecipientGroup> findById(Long id) throws IOException;

    /**
     * Find recipient group by name
     */
    Optional<RecipientGroup> findByName(String name) throws IOException;

    /**
     * Get all recipient groups
     */
    List<RecipientGroup> findAll() throws IOException;

    /**
     * Delete recipient group by ID
     */
    void deleteById(Long id) throws IOException;

    /**
     * Delete recipient group by name
     */
    void deleteByName(String name) throws IOException;
}
