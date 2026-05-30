package com.chatpress.artifact;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ArtifactServiceTransactionTest {

    @Autowired
    private ArtifactService artifactService;

    @Autowired
    private ArtifactMapper artifactMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void createArtifactIsCommitted() {
        Artifact artifact = artifactService.createArtifact("Tx Test", "# Tx Test", null, "tester");

        Artifact found = artifactMapper.selectById(artifact.getId());
        assertThat(found.getTitle()).isEqualTo("Tx Test");
    }

    @Test
    void manualRollbackDoesNotPersist() {
        TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Artifact artifact = new Artifact(
                "Rollback Test", "rollback-test", "# Rollback", "<p>Rollback</p>", "tester"
        );
        artifactMapper.insert(artifact);

        transactionManager.rollback(tx);

        assertThat(artifactMapper.findBySlug("rollback-test")).isEmpty();
    }

    @Test
    void slugUniqueConstraintPreventsDuplicate() {
        Artifact first = new Artifact("Dup Slug", "dup-slug", "# Dup", "<p>Dup</p>", "tester");
        artifactMapper.insert(first);

        Artifact second = new Artifact("Dup Slug 2", "dup-slug", "# Dup 2", "<p>Dup 2</p>", "tester");
        assertThatThrownBy(() -> artifactMapper.insert(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteArtifactIsCommitted() {
        Artifact artifact = artifactService.createArtifact("Delete Tx", "# Delete Tx", null, "tester");
        Long id = artifact.getId();

        artifactService.deleteArtifactOrThrow(id, "tester");

        assertThat(artifactMapper.selectById(id)).isNull();
    }

    @Test
    void v4MigrationIndexesApplied() {
        artifactService.createArtifact("Index Test", "# Index Test", null, "tester");

        var results = artifactService.listArtifacts(0, 10, "Index", "published", null, "tester");
        assertThat(results.getTotal()).isEqualTo(1);
    }
}
