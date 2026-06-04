package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * RecipientGroup entity for saving frequently used email recipient lists
 * Users can create groups like "Team", "Clients", "Management" and reuse them quickly
 */
public class RecipientGroup {
    public Long id;
    public String name;           // Group name (e.g., "Development Team", "Clients")
    public String description;    // Optional group description
    public Set<String> emails;    // Set of email addresses in group (comma-separated in DB)
    public Integer emailCount;    // Number of emails in group
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // Constructors
    public RecipientGroup() {}

    public RecipientGroup(String name, String description, Set<String> emails) {
        this.name = name;
        this.description = description;
        this.emails = emails;
        this.emailCount = emails != null ? emails.size() : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RecipientGroup(Long id, String name, String description, Set<String> emails) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emails = emails;
        this.emailCount = emails != null ? emails.size() : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name + " (" + emailCount + " recipients)";
    }
}
