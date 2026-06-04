package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.RecipientGroupDao;
import nlu.fit.soft.gr5.precisionMail.model.RecipientGroup;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO Implementation for RecipientGroup operations
 * Manages recipient groups in SQLite database
 */
public class RecipientGroupDaoImpl implements RecipientGroupDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientGroupDaoImpl.class);

    private static final String TABLE_NAME = "recipient_groups";

    @Override
    public RecipientGroup save(RecipientGroup group) throws IOException {
        String sql = "INSERT INTO " + TABLE_NAME + " (name, description, emails, email_count, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, group.name);
            pstmt.setString(2, group.description);
            pstmt.setString(3, String.join("\n", group.emails != null ? group.emails : Set.of()));
            pstmt.setInt(4, group.emailCount);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        group.id = rs.getLong(1);
                        LOGGER.info("Recipient group saved successfully. name={}, emailCount={}", group.name, group.emailCount);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to save recipient group.", e);
            throw new IOException("Failed to save recipient group: " + e.getMessage(), e);
        }

        return group;
    }

    @Override
    public RecipientGroup update(RecipientGroup group) throws IOException {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ?, description = ?, emails = ?, email_count = ?, updated_at = ? " +
                "WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.name);
            pstmt.setString(2, group.description);
            pstmt.setString(3, String.join("\n", group.emails != null ? group.emails : Set.of()));
            pstmt.setInt(4, group.emailCount);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(6, group.id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Recipient group updated successfully. id={}, name={}", group.id, group.name);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to update recipient group.", e);
            throw new IOException("Failed to update recipient group: " + e.getMessage(), e);
        }

        return group;
    }

    @Override
    public Optional<RecipientGroup> findById(Long id) throws IOException {
        String sql = "SELECT id, name, description, emails, email_count, created_at, updated_at FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGroup(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to find recipient group by id.", e);
            throw new IOException("Failed to find recipient group: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<RecipientGroup> findByName(String name) throws IOException {
        String sql = "SELECT id, name, description, emails, email_count, created_at, updated_at FROM " + TABLE_NAME + " WHERE name = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGroup(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to find recipient group by name.", e);
            throw new IOException("Failed to find recipient group: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<RecipientGroup> findAll() throws IOException {
        List<RecipientGroup> groups = new ArrayList<>();
        String sql = "SELECT id, name, description, emails, email_count, created_at, updated_at FROM " + TABLE_NAME +
                " ORDER BY updated_at DESC LIMIT 1000";

        try (Connection conn = DbUtil.getConnect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
            LOGGER.info("Retrieved {} recipient groups from database.", groups.size());
        } catch (SQLException e) {
            LOGGER.error("Failed to retrieve recipient groups.", e);
            throw new IOException("Failed to retrieve recipient groups: " + e.getMessage(), e);
        }

        return groups;
    }

    @Override
    public void deleteById(Long id) throws IOException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Recipient group deleted successfully. id={}", id);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to delete recipient group.", e);
            throw new IOException("Failed to delete recipient group: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByName(String name) throws IOException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE name = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Recipient group deleted successfully. name={}", name);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to delete recipient group.", e);
            throw new IOException("Failed to delete recipient group: " + e.getMessage(), e);
        }
    }

    private RecipientGroup mapResultSetToGroup(ResultSet rs) throws SQLException {
        RecipientGroup group = new RecipientGroup();
        group.id = rs.getLong("id");
        group.name = rs.getString("name");
        group.description = rs.getString("description");

        // Parse emails from newline-separated string
        String emailsStr = rs.getString("emails");
        if (emailsStr != null && !emailsStr.isEmpty()) {
            group.emails = new HashSet<>(Arrays.asList(emailsStr.split("\n")));
        } else {
            group.emails = new HashSet<>();
        }

        group.emailCount = rs.getInt("email_count");
        group.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        group.updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();

        return group;
    }
}
