package com.chatpress.artifact;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ArtifactMapper extends BaseMapper<Artifact> {

    Optional<Artifact> findBySlug(@Param("slug") String slug);

    Optional<Artifact> findBySlugAndStatus(@Param("slug") String slug,
                                            @Param("status") String status);

    List<String> findSlugsByPrefix(@Param("prefix") String prefix);

    Page<Artifact> findPublishedByPage(IPage<Artifact> page);

    Page<Artifact> searchArtifacts(IPage<Artifact> page,
                                    @Param("username") String username,
                                    @Param("keyword") String keyword,
                                    @Param("tag") String tag,
                                    @Param("status") String status);
}
