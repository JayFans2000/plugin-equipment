package com.kunkunyu.equipment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 装备页面单例扩展
 * 用于作为装备页面评论的主体资源，使用独立的 group 和 kind 避免与其他插件冲突
 *
 * @author Cyan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "equipment.kunkunyu.com", version = "v1alpha1", kind = "EquipmentPage",
    plural = "equipmentpages", singular = "equipmentpage")
public class EquipmentPage extends AbstractExtension {

    public static final String SINGLETON_NAME = "equipment";

    private EquipmentPageSpec spec;

    @Data
    public static class EquipmentPageSpec {
        private String title;
        private String description;
    }
}
