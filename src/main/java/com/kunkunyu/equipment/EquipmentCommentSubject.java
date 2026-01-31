package com.kunkunyu.equipment;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentSubject;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.ExternalLinkProcessor;

/**
 * 装备页面评论主体处理类
 * 使用自定义的 EquipmentPage 类型作为评论主体
 *
 * @author Cyan
 */
@Component
@RequiredArgsConstructor
class EquipmentCommentSubject implements CommentSubject<EquipmentPage> {

    private static final GroupVersionKind GVK = GroupVersionKind.fromExtension(EquipmentPage.class);

    private final ReactiveExtensionClient client;
    private final ExternalLinkProcessor externalLinkProcessor;

    @Override
    public Mono<EquipmentPage> get(String name) {
        return client.fetch(EquipmentPage.class, name)
            .switchIfEmpty(Mono.defer(() -> client.create(createDefaultEquipmentPage(name))));
    }

    @Override
    public Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return get(name).map(page -> {
            var url = externalLinkProcessor.processLink("/equipments");
            return new SubjectDisplay("装备页面", url, "装备");
        });
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        return Objects.equals(GVK.group(), ref.getGroup())
            && Objects.equals(GVK.kind(), ref.getKind());
    }

    private EquipmentPage createDefaultEquipmentPage(String name) {
        var page = new EquipmentPage();
        var metadata = new Metadata();
        metadata.setName(name);
        page.setMetadata(metadata);
        var spec = new EquipmentPage.EquipmentPageSpec();
        spec.setTitle("装备页面");
        spec.setDescription("装备展示页面");
        page.setSpec(spec);
        return page;
    }
}
