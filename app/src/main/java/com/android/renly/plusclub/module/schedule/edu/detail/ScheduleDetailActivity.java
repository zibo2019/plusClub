package com.android.renly.plusclub.module.schedule.edu.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.renly.plusclub.api.bean.Course;
import com.android.renly.plusclub.module.base.BaseActivity;
import com.android.renly.plusclub.R;
import com.android.renly.plusclub.module.schedule.edu.set.ScheduleDetailSetActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class ScheduleDetailActivity extends BaseActivity {
    @BindView(R.id.tv_schedule_setCourseName)
    TextView tvScheduleSetCourseName;
    @BindView(R.id.tv_schedule_setClass)
    TextView tvScheduleSetClass;
    @BindView(R.id.tv_schedule_setWeeks)
    TextView tvScheduleSetWeeks;
    @BindView(R.id.tv_schedule_setSDweek)
    TextView tvScheduleSetSDweek;
    @BindView(R.id.tv_schedule_setJS)
    TextView tvScheduleSetJS;
    @BindView(R.id.tv_schedule_setTeacher)
    TextView tvScheduleSetTeacher;

    private Course object;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_schedule_detail;
    }

    @Override
    protected void initData() {
        initClassObject();
        initBaseData();
    }

    @Override
    protected void initView() {
        initToolBar(true, "编辑课程");
        initSlidr();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 初始化课程对象
     */
    private void initClassObject() {
        Intent intent = getIntent();
        String JsonObj = intent.getStringExtra("JsonObj");
        object = JSON.parseObject(JsonObj,Course.class);
    }

    /**
     * 初始化基础数据
     */
    private void initBaseData() {
        tvScheduleSetCourseName.setText(object.getCourseName());
        tvScheduleSetClass.setText(object.getClassRoom());
        tvScheduleSetWeeks.setText(object.getStartWeek() + " - " + object.getEndWeek() + "周");
        tvScheduleSetSDweek.setText(object.printSD_week());
        tvScheduleSetJS.setText(object.getCourseTime());
        tvScheduleSetTeacher.setText(object.getTeacher());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK){
            switch (resultCode){
                case ScheduleDetailSetActivity.requestCode:
                    String JsonObj = data.getStringExtra("JsonObj");
                    object = JSON.parseObject(JsonObj,Course.class);
                    initBaseData();
                    break;
            }
        }
    }

    @OnClick({R.id.fl_setCourseName, R.id.fl_setClass, R.id.fl_setWeeks, R.id.fl_setSDweek, R.id.fl_setJS, R.id.fl_setTeacher})
    public void onViewClicked(View view) {
        Intent intent = new Intent(this,ScheduleDetailSetActivity.class);
        intent.putExtra("JsonObj",JSON.toJSONString(object));
        switch (view.getId()) {
            case R.id.fl_setCourseName:
                intent.putExtra("set","courseName");
                break;
            case R.id.fl_setClass:
                intent.putExtra("set","class");
                break;
            case R.id.fl_setWeeks:
                intent.putExtra("set","weeks");
                break;
            case R.id.fl_setSDweek:
                intent.putExtra("set","sdWeek");
                break;
            case R.id.fl_setJS:
                intent.putExtra("set","js");
                break;
            case R.id.fl_setTeacher:
                intent.putExtra("set","teacher");
                break;
        }
        startActivityForResult(intent, ScheduleDetailSetActivity.requestCode);
    }
}
