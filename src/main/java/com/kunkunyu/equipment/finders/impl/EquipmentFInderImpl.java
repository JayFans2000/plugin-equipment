package com.kunkunyu.equipment.finders.impl;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.theme.finders.Finder;
import com.kunkunyu.equipment.Equipment;
import com.kunkunyu.equipment.EquipmentGroup;
import com.kunkunyu.equipment.finders.EquipmentFinder;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;

@Finder("equipmentFinder")
public class EquipmentFInderImpl implements EquipmentFinder {
    private final ReactiveExtensionClient client;

    public EquipmentFInderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Flux<EquipmentVo> listAll() {
        return this.client.listAll(Equipment.class, ListOptions.builder().build(), defaultSort())
            .map(EquipmentVo::from);
    }

    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }

    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size, String group) {
        return pageEquipment(page, size, group);
    }

    private Mono<ListResult<EquipmentVo>> pageEquipment(Integer page, Integer size, String group) {
        var builder = ListOptions.builder();
        if (StringUtils.isNotEmpty(group)) {
            builder.andQuery(Queries.equal("spec.groupName", group));
        }
        return client.listBy(Equipment.class, builder.build(),
                PageRequestImpl.of(page, size, defaultSort()))
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .map(EquipmentVo::from)
                .collectList()
                .map(list -> new ListResult<>(
                    listResult.getPage(), listResult.getSize(), listResult.getTotal(), list
                ))
            );
    }

    @Override
    public Flux<EquipmentVo> listBy(String groupName) {
        var options = ListOptions.builder()
            .andQuery(Queries.equal("spec.groupName", groupName))
            .build();
        return client.listAll(Equipment.class, options, defaultSort()).map(EquipmentVo::from);
    }

    @Override
    public Flux<EquipmentGroupVo> groupBy() {
        return this.client.listAll(EquipmentGroup.class, ListOptions.builder().build(), defaultSort())
            .concatMap(group -> {
                var builder = EquipmentGroupVo.from(group);
                return this.listBy(group.getMetadata().getName())
                    .collectList()
                    .doOnNext(equipments -> {
                        EquipmentGroup.EquipmentGroupStatus status = group.getStatus();
                        status.setEquipmentCount(equipments.size());
                        builder.status(status);
                        builder.equipments(equipments);
                    })
                    .then(Mono.fromSupplier(builder::build));
            });
    }

    static Sort defaultSort() {
        return Sort.by(
            asc("spec.priority"),
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }

}
