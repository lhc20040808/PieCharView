package lhc.piecharview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lhc.piecharview.bean.Pie;
import lhc.piecharview.view.PieCharView;

public class MainActivity extends AppCompatActivity {
    private PieCharView pieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pieView = (PieCharView) findViewById(R.id.pie);
        Pie pie = new Pie();
        pie.setPer(45);
        pie.setName("类型4");
        pieView.addPie("类型1", 10).addPie("类型2", 30).addPie(pie).addPie("类型3", 15).setRiskType("文字").draw();
    }
}
