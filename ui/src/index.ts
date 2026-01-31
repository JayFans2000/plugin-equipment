import { definePlugin, type CommentSubjectRefProvider, type CommentSubjectRefResult } from "@halo-dev/ui-shared";
import "uno.css";
import { markRaw } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3';

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/equipments",
        name: "Equipments",
        component: () => import("@/views/EquipmentList.vue"),
        meta: {
          permissions: ["plugin:equipment:view"],
          menu: {
            name: "装备",
            group: "content",
            icon: markRaw(TablerDeviceGamepad3),
          },
        },
      },
    },
  ],
  extensionPoints: {
    "comment:subject-ref:create": (): CommentSubjectRefProvider[] => {
      return [
        {
          kind: "EquipmentPage",
          group: "equipment.kunkunyu.com",
          resolve: (): CommentSubjectRefResult => {
            return {
              label: "装备",
              title: "装备页面",
              externalUrl: "/equipments",
              route: {
                name: "Equipments",
              },
            };
          },
        },
      ];
    },
  },
});
