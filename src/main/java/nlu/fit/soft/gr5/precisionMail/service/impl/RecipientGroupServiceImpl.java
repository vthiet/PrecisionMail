package nlu.fit.soft.gr5.precisionMail.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nlu.fit.soft.gr5.precisionMail.dao.RecipientGroupDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.RecipientGroupDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.RecipientGroup;
import nlu.fit.soft.gr5.precisionMail.service.RecipientGroupService;

/**
 * Implementation of RecipientGroupService
 */
public class RecipientGroupServiceImpl implements RecipientGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientGroupServiceImpl.class);
    private final RecipientGroupDao dao = new RecipientGroupDaoImpl();

    @Override
    public RecipientGroup createGroup(String name, String description, Set<String> emails) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        RecipientGroup group = new RecipientGroup(name, description, emails);
        LOGGER.info("Creating recipient group: name={}, emailCount={}", name, emails != null ? emails.size() : 0);
        return dao.save(group);
    }

    @Override
    public RecipientGroup updateGroup(Long id, String name, String description, Set<String> emails) throws IOException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Group ID must be valid");
        }

        RecipientGroup group = new RecipientGroup(id, name, description, emails);
        LOGGER.info("Updating recipient group: id={}, name={}, emailCount={}", id, name, emails != null ? emails.size() : 0);
        return dao.update(group);
    }

    @Override
    public List<RecipientGroup> getAllGroups() throws IOException {
        return dao.findAll();
    }

    @Override
    public Optional<RecipientGroup> getGroupById(Long id) throws IOException {
        return dao.findById(id);
    }

    @Override
    public Optional<RecipientGroup> getGroupByName(String name) throws IOException {
        return dao.findByName(name);
    }

    @Override
    public void deleteGroup(Long id) throws IOException {
        LOGGER.info("Deleting recipient group: id={}", id);
        dao.deleteById(id);
    }

    @Override
    public void deleteGroupByName(String name) throws IOException {
        LOGGER.info("Deleting recipient group: name={}", name);
        dao.deleteByName(name);
    }

    @Override
    public void addEmailToGroup(Long id, String email) throws IOException {
        Optional<RecipientGroup> groupOpt = dao.findById(id);
        if (groupOpt.isPresent()) {
            RecipientGroup group = groupOpt.get();
            if (group.emails.add(email)) {
                group.emailCount = group.emails.size();
                dao.update(group);
                LOGGER.info("Email added to group. id={}, email={}", id, email);
            }
        } else {
            throw new IllegalArgumentException("Group with ID " + id + " not found");
        }
    }

    @Override
    public void removeEmailFromGroup(Long id, String email) throws IOException {
        Optional<RecipientGroup> groupOpt = dao.findById(id);
        if (groupOpt.isPresent()) {
            RecipientGroup group = groupOpt.get();
            if (group.emails.remove(email)) {
                group.emailCount = group.emails.size();
                dao.update(group);
                LOGGER.info("Email removed from group. id={}, email={}", id, email);
            }
        } else {
            throw new IllegalArgumentException("Group with ID " + id + " not found");
        }
    }
}
