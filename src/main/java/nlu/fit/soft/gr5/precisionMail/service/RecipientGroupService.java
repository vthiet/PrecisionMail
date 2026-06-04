package nlu.fit.soft.gr5.precisionMail.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import nlu.fit.soft.gr5.precisionMail.model.RecipientGroup;

/**
 * Service Interface for RecipientGroup operations
 */
public interface RecipientGroupService {
    /**
     * Create and save a new recipient group
     */
    RecipientGroup createGroup(String name, String description, Set<String> emails) throws IOException;

    /**
     * Update existing group
     */
    RecipientGroup updateGroup(Long id, String name, String description, Set<String> emails) throws IOException;

    /**
     * Get all recipient groups
     */
    List<RecipientGroup> getAllGroups() throws IOException;

    /**
     * Get group by ID
     */
    Optional<RecipientGroup> getGroupById(Long id) throws IOException;

    /**
     * Get group by name
     */
    Optional<RecipientGroup> getGroupByName(String name) throws IOException;

    /**
     * Delete group by ID
     */
    void deleteGroup(Long id) throws IOException;

    /**
     * Delete group by name
     */
    void deleteGroupByName(String name) throws IOException;

    /**
     * Add email to existing group
     */
    void addEmailToGroup(Long id, String email) throws IOException;

    /**
     * Remove email from group
     */
    void removeEmailFromGroup(Long id, String email) throws IOException;
}
