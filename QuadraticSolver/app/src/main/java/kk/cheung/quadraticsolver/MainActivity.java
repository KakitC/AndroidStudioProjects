package kk.cheung.quadraticsolver;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void solve(View view) {
        TextView x1root = (TextView) findViewById(R.id.x1root);
        TextView x2root = (TextView) findViewById(R.id.x2root);
        String x1 = "";
        String x2 = "";

        //Get input fields to double values
        EditText Ax2Field = (EditText) findViewById(R.id.fieldA);
        EditText BxField = (EditText) findViewById(R.id.fieldB);
        EditText CField = (EditText) findViewById(R.id.fieldC);

        try { //Check if they're numbers
            double Ax2 = Double.parseDouble(Ax2Field.getText().toString());
            double Bx = Double.parseDouble(BxField.getText().toString());
            double C = Double.parseDouble(CField.getText().toString());

            if (Ax2 != 0) { //Quadratic case
                double roots[] = solveQuadratic(Ax2, Bx, C);
                if (roots[1] != 0) { //Complex roots
                    x1 = String.format("%.4f", roots[0]) + " + i*" + String.format("%.4f", roots[1]);
                    x2 = String.format("%.4f", roots[2]) + " - i*" + String.format("%.4f", roots[3]);
                } else { //Real roots
                    x1 = String.format("%.4f", roots[0]);
                    x2 = String.format("%.4f", roots[2]);
                }

            } else if (Bx != 0) { //0 x^2 term, linear case
                x1 = String.format("%.4f", -C/Bx);
                x2 = "Linear equation only";

            } else { //No equation or just const. = 0
                x1 = "NaN";
                x2 = "NaN";
            }

        } catch (NumberFormatException e) { //These are not numbers
            x1 = "NaN";
            x2 = "NaN";
        }

        x1root.setText(x1);
        x2root.setText(x2);
    }

    private double[] solveQuadratic(double a, double b, double c) {
        // Solves quadratic equation and returns as an array: real 1, complex 1, real 2, complex 2
        double[] roots = new double[4];
        double threshold = 0.001;

        if (b*b > 4*a*c + threshold) { //Real distinct roots
            roots[0] = (-b + Math.sqrt(b*b - 4*a*c))/a/2.0;
            roots[1] = 0;
            roots[2] = (-b - Math.sqrt(b*b - 4*a*c))/a/2.0;
            roots[3] = 0;
        } else if (b*b < 4*a*c - threshold) { //Complex roots
            roots[0] = -b/a/2.0;
            roots[1] = Math.sqrt(4*a*c - b*b)/a/2.0;
            roots[2] = -b/a/2.0;
            roots[3] = Math.sqrt(4*a*c - b*b)/a/2.0;
        } else { //Equal roots
            roots[0] = -b/a/2.0;
            roots[1] = 0;
            roots[2] = roots[1];
            roots[3] = 0;
        }

        return roots;
    }
}
