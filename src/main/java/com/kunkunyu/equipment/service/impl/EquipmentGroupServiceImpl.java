package com.kunkunyu.equipment.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import com.kunkunyu.equipment.Equipment;
import com.kunkunyu.equipment.EquipmentGroup;
import com.kunkunyu.equipment.service.EquipmentGroupService;

/**
 * Service implementation for {@link Equipment}.
 *
 */
@Component
class EquipmentGroupServiceImpl implements EquipmentGroupService {

    private final ReactiveExtensionClient client;

    public EquipmentGroupServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<ListResult<EquipmentGroup>> listEquipmentGroup(QueryListRequest query) {
        return this.client.listBy(
                EquipmentGroup.class,
                toListOptions(query),
                PageRequestImpl.of(query.getPage(), query.getSize())
            )
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .flatMap(this::populateEquipments)
                .collectList()
                .map(groups -> new ListResult<>(
                    listResult.getPage(),
                    listResult.getSize(),
                    listResult.getTotal(),
                    groups
                ))
            );
    }

    @Override
    public Mono<EquipmentGroup> deleteEquipmentGroup(String name) {
        return this.client.fetch(EquipmentGroup.class, name)
            .flatMap(this.client::delete)
            .flatMap(deleted -> {
                    var listOptions = ListOptions.builder()
                        .andQuery(Queries.equal("spec.groupName", name))
                        .build();
                    return this.client.listAll(Equipment.class, listOptions, Sort.unsorted())
                        .flatMap(this.client::delete)
                        .then()
                        .thenReturn(deleted);
                }
            );
    }

    private Mono<EquipmentGroup> populateEquipments(EquipmentGroup equipmentGroup) {
        return fetchEquipmentCount(equipmentGroup)
            .doOnNext(count -> equipmentGroup.getStatusOrDefault().setEquipmentCount(count))
            .thenReturn(equipmentGroup);
    }

    Mono<Integer> fetchEquipmentCount(EquipmentGroup equipmentGroup) {
        Assert.notNull(equipmentGroup, "The equipmentGroup must not be null.");
        String name = equipmentGroup.getMetadata().getName();
        return client.list(
                Equipment.class,
                equipment -> !equipment.isDeleted() && equipment.getSpec().getGroupName().equals(name),
                null
            )
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }

    ListOptions toListOptions(QueryListRequest query) {
        return labelAndFieldSelectorToListOptions(
            query.getLabelSelector(), query.getFieldSelector()
        );
    }
}
