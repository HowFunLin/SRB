<template>
  <div class="app-container">
    <!-- 输入表单 -->
    <el-form label-width="120px">
      <el-form-item label="借款额度">
        <el-input-number v-model="integralGrade.borrowAmount" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间开始">
        <el-input-number v-model="integralGrade.integralStart" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间结束">
        <el-input-number v-model="integralGrade.integralEnd" :min="0" />
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="saveBtnDisabled"
          type="primary"
          @click="saveOrUpdate()"
        >
          保存
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
// 引入 API 模块
import integralGradeApi from "@/api/core/integral-grade";

export default {
  data() {
    return {
      integralGrade: {}, // 初始化数据
      saveBtnDisabled: false, // 默认不禁用保存按钮
    };
  },
  created() {
    //页面渲染成功
    if (this.$route.params.id) {
      this.fetchDataById(this.$route.params.id);
    }
  },
  methods: {
    saveOrUpdate() {
      // 禁用保存按钮，防止表单重复提交
      this.saveBtnDisabled = true;

      if (this.integralGrade.id) this.updateData();
      else this.saveData();
    },

    // 新增数据
    saveData() {
      integralGradeApi.save(this.integralGrade).then((response) => {
        this.$message.success(response.message);
        this.$router.push("/core/integral-grade/list"); // 路由跳转
      });
    },

    // 根据id查询记录
    fetchDataById(id) {
      integralGradeApi.getById(id).then((response) => {
        this.integralGrade = response.data.record;
      });
    },

    // 根据id更新记录
    updateData() {
      // 数据的获取
      integralGradeApi.updateById(this.integralGrade).then((response) => {
        this.$message.success(response.message);
        this.$router.push("/core/integral-grade/list");
      });
    },
  },
};
</script>

<style scoped></style>
