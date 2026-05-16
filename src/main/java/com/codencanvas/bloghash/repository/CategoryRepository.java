package com.codencanvas.bloghash.repository;

import com.codencanvas.bloghash.domain.post.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
 
    Optional<Category> findBySlug(String slug);
 
    // Root categories (for sidebar/navigation)
    List<Category> findByParentIsNullOrderByNameAsc();
 
    // Children of a category
    List<Category> findByParentIdOrderByNameAsc(UUID parentId);
}
 
