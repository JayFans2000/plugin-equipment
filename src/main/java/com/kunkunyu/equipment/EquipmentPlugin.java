package com.kunkunyu.equipment;


import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import java.util.Optional;

@Component
public class EquipmentPlugin extends BasePlugin {
    private final SchemeManager schemeManager;

    public EquipmentPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(Equipment.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Equipment, String>single("spec.groupName", String.class)
                .indexFunc(
                    equipment -> Optional.ofNullable(equipment.getSpec())
                        .map(Equipment.EquipmentSpec::getGroupName)
                        .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Equipment, String>single("spec.displayName", String.class)
                .indexFunc(
                    equipment -> Optional.ofNullable(equipment.getSpec())
                        .map(Equipment.EquipmentSpec::getDisplayName)
                        .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Equipment, Integer>single("spec.priority", Integer.class)
                .indexFunc(
                    equipment -> Optional.ofNullable(equipment.getSpec())
                        .map(Equipment.EquipmentSpec::getPriority)
                        .orElse(0)
                )
            );
        });
        schemeManager.register(EquipmentGroup.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<EquipmentGroup, Integer>single("spec.priority", Integer.class)
                .indexFunc(
                    group -> Optional.ofNullable(group.getSpec())
                        .map(EquipmentGroup.EquipmentGroupSpec::getPriority)
                        .orElse(0)
                )
            );
        });
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(Equipment.class));
        schemeManager.unregister(Scheme.buildFromType(EquipmentGroup.class));
    }
}
