<script lang="ts" setup>
import type { Equipment } from "@/types";
import { axiosInstance } from "@halo-dev/api-client";
import { VSpace, VButton, VModal } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import {computed, nextTick, onMounted, ref, useTemplateRef} from "vue";

const props = withDefaults(
  defineProps<{
    equipment?: Equipment;
    group?: string;
  }>(),
  {
    equipment: undefined,
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", equipment: Equipment): void;
}>();

const initialFormState: Equipment = {
  metadata: {
    name: "",
    generateName: "equipment-",
  },
  spec: {
    displayName: "",
    cover: "",
    groupName: props.group || "",
  },
  kind: "Equipment",
  apiVersion: "equipment.kunkunyu.com/v1alpha1",
} as Equipment;

const formState = ref<Equipment>(cloneDeep(initialFormState));
const isSubmitting = ref(false);
const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑装备" : "添加装备";
});

onMounted(() => {
  if (props.equipment) {
    formState.value = cloneDeep(props.equipment);
  }
});

const annotationsFormRef = ref();

const handleSaveEquipment = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsFormRef.value || {};
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
      await axiosInstance.put<Equipment>(
        `/apis/equipment.kunkunyu.com/v1alpha1/equipments/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      if (props.group) {
        formState.value.spec.groupName = props.group;
      }
      const { data } = await axiosInstance.post<Equipment>(`/apis/equipment.kunkunyu.com/v1alpha1/equipments`, formState.value);
      emit("saved", data);
    }
    modal.value?.close();
  } catch (e) {
    console.error(e);
  } finally {
    isSubmitting.value = false;
  }
};
</script>
<template>
  <VModal ref="modal" :title="modalTitle" :width="650" @close="emit('close')">
    <template #actions>
      <slot name="append-actions" />
    </template>

    <FormKit
      id="equipment-form"
      v-model="formState.spec"
      name="equipment-form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveEquipment"
    >
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base text-gray-900 font-medium"> 常规 </span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0 divide-y divide-gray-100">
          <FormKit name="displayName" label="名称" type="text" validation="required"></FormKit>
          <FormKit name="cover" label="封面" type="attachment" :accepts="['image/*']"></FormKit>
          <FormKit name="url" label="装备地址" type="text" :accepts="['image/*']"></FormKit>
          <FormKit name="specification" label="装备规格" type="text" ></FormKit>
          <FormKit name="description" label="描述" type="textarea"></FormKit>
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
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Equipment"
          group="equipment.kunkunyu.com"
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton :loading="isSubmitting" type="secondary"
                 @click="$formkit.submit('equipment-form')"> 保存 </VButton>
        <VButton @click="modal?.close()">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
