<script lang="ts" setup>
import type { EquipmentGroup } from "@/types";
import { axiosInstance } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import {computed, nextTick, onMounted, ref, useTemplateRef, watch} from "vue";

const props = withDefaults(
  defineProps<{
    group?: EquipmentGroup;
  }>(),
  {
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const initialFormState: EquipmentGroup = {
  apiVersion: "equipment.kunkunyu.com/v1alpha1",
  kind: "EquipmentGroup",
  metadata: {
    name: "",
    generateName: "equipment-group-",
  },
  spec: {
    displayName: "",
    priority: 0,
  },
  status: {
    equipmentCount: 0,
  },
};

const formState = ref<EquipmentGroup>(initialFormState);
const isSubmitting = ref(false);
const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);
const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑分组" : "新建分组";
});
const annotationsGroupFormRef = ref();

const handleCreateOrUpdateGroup = async () => {
  annotationsGroupFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsGroupFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }
  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };
  try {
    isSubmitting.value = true;
    if (isUpdateMode.value) {
      await axiosInstance.put(
        `/apis/equipment.kunkunyu.com/v1alpha1/equipmentgroups/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      await axiosInstance.post("/apis/equipment.kunkunyu.com/v1alpha1/equipmentgroups", formState.value);
    }
    modal.value?.close();
  } catch (e) {
    console.error("Failed to create equipment group", e);
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  if (props.group) {
    formState.value = cloneDeep(props.group);
  }
});
</script>
<template>
  <VModal ref="modal" :width="600" :title="modalTitle" @close="emit('close')">
    <FormKit
      id="equipment-group-form"
      v-model="formState.spec"
      name="equipment-group-form"
      type="form"
      @submit="handleCreateOrUpdateGroup"
    >
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base text-gray-900 font-medium"> 常规 </span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0 divide-y divide-gray-100">
          <FormKit
            name="displayName"
            label="分组名称"
            type="text"
            validation="required"
            help="可根据此名称查询装备"
          ></FormKit>
          <FormKit
            name="description"
            label="分组描述"
            type="textarea"
          ></FormKit>
          
        </div>
      </div>
    </FormKit>
    <div class=":uno: py-5">
      <div class=":uno: border-t border-gray-200"></div>
    </div>
    <div class=":uno: md:grid md:grid-cols-4 md:gap-6">
      <div class=":uno: md:col-span-1">
        <div class=":uno: sticky top-0">
          <span class=":uno: text-base text-gray-900 font-medium"> 元数据 </span>
        </div>
      </div>
      <div class=":uno: mt-5 md:col-span-3 md:mt-0 divide-y divide-gray-100">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsGroupFormRef"
          :value="formState.metadata.annotations"
          kind="EquipmentGroup"
          group="equipment.kunkunyu.com"
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton :loading="isSubmitting" type="secondary" @click="$formkit.submit('equipment-group-form')">
          提交
        </VButton>
        <VButton @click="emit('close')">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
