<template>
  <div class="app-container">
    <!-- 顶层搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="等级名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入等级名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" style="width: 240px;" clearable>
          <el-option
          v-for="item in nursing_level_status"
          :key="item.value"
          :label="item.label"
          :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <!-- 二次按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['nursing:level:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <!-- 表格栏名称 -->
    <el-table v-loading="loading" :data="levelList" @selection-change="handleSelectionChange">
      <el-table-column label="序号" type="index" align="center" />
      <el-table-column label="等级名称" align="center" prop="name" />
      <el-table-column label="护理计划" align="center" prop="planName" />
      <el-table-column label="护理费用(元/月)" align="center" prop="fee" />
      <el-table-column label="状态" align="center" prop="status" >
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:level:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:level:remove']">删除</el-button>
          <el-button link type="primary" :icon="scope.row.status == 0 ? 'Lock' : 'Unlock'" @click="handleEnable(scope.row)" >{{ scope.row.status == 0 ? '启用' : '禁用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改护理等级对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="levelRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="护理计划ID" prop="planId">
          <el-select v-model="form.planId" placeholder="请输入护理计划ID" clearable>
            <el-option
            v-for="item in planOptions"
            :key="item.id"
            :label="item.planName"
            :value="item.id"/>
          </el-select>
        </el-form-item> 
          <el-form-item label="护理费用" prop="fee">
            <el-input-number v-model="form.fee" 
            placeholder="请输入护理费用" 
            :min="5" 
            :step="5" />
        </el-form-item>   
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio 
            v-for="dict in nursing_level_status"
            :key="dict.value"
            :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Level">
import { listLevel, getLevel, delLevel, addLevel, updateLevel } from "@/api/nursing/level"
import { listPlan } from "@/api/nursing/plan";
const { proxy } = getCurrentInstance()

const levelList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const { nursing_level_status } = proxy.useDict("nursing_level_status");
const planOptions = ref([]);
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    status: null,
  },
  rules: {
  }
})

// 加一个取数据的函数
function getPlanOptions() {
  listPlan({ pageNum: 1, pageSize: 1000 }).then(res => {
    planOptions.value = res.rows;
  });
}

// 页面加载时调用
onMounted(() => {
  getPlanOptions();
});

const { queryParams, form, rules } = toRefs(data)

/** 查询护理等级列表 */
function getList() {
  loading.value = true
  listLevel(queryParams.value).then(response => {
    levelList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null,
    planId: null,
    fee: null,
    status: null,
    description: null,
    remark: null,
    createTime: null,
    updateTime: null,
    createBy: null,
    updateBy: null
  }
  proxy.resetForm("levelRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加护理等级"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getLevel(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改护理等级"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["levelRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateLevel(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addLevel(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/* 启用禁用按钮操作 */
function handleEnable(row) {
  const status = row.status;
  const msg = status === 1 ? '禁用' : '启用';
  const params = {
    id: row.id,
    status: status === 1 ? 0 : 1
  };
  proxy.$modal.confirm(`是否确认${msg}护理等级的数据项？`).then(function() {
    return updateLevel(params);
  }).then(() => {
    proxy.$modal.msgSuccess(`${msg}成功`);
    getList();
  }).catch(() => {});
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除护理等级编号为"' + _ids + '"的数据项？').then(function() {
    return delLevel(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('nursing/level/export', {
    ...queryParams.value
  }, `level_${new Date().getTime()}.xlsx`)
}

getList()
</script>
