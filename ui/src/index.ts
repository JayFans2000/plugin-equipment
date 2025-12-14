import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3'
import "uno.css";

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
});
