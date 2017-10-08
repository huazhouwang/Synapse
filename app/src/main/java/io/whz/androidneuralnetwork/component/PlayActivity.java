package io.whz.androidneuralnetwork.component;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.DigitView;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.neural.NeuralNetwork;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.util.DbHelper;

public class PlayActivity extends AppCompatActivity {

    private DigitView mDigitView;
    private TextView mPredictText;
    private NeuralNetwork mNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mDigitView = findViewById(R.id.digit_view);
        mPredictText = findViewById(R.id.predict);

        setUpActionBar();
        prepare();
    }

    private void prepare() {
        final List<Model> models = Global.getInstance()
                .getSession().getModelDao().queryBuilder().orderDesc(ModelDao.Properties.CreatedTime).listLazy();

        final Model model = models.get(0);

        model.setWeights(DbHelper.byteArray2MatrixArray(model.getWeightBytes()));
        model.setBiases(DbHelper.byteArray2MatrixArray(model.getBiasBytes()));

        mNetwork = new NeuralNetwork(model.getWeights(), model.getBiases());
    }

    private void setUpActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAfterTransition();
        return true;
    }

    public void play(View view) {
        try {
            final int n = mNetwork.predict(mDigitView.getDarkness());
            mPredictText.setText(String.valueOf(n));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
