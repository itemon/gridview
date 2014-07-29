package com.gv.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements OnItemClickListener {

    /**
	 * @author itemon.huang
	 *
	 */
	private final class TestAdapter extends BaseAdapter {
		/**
		 * 
		 */
		public TestAdapter() {
			mElementsData = new String[] {
				"hello", "world", "the", "lazy",
				"dog", "jumps", "over", "the", "fox"
			};
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return mElementsData.length;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int arg0) {
			return mElementsData[arg0];
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = arg1;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.griditem, arg2, false);
			}
			((TextView)view).setText(mElementsData[arg0]);
			return view;
		}

		private String[] mElementsData;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.i(TAG, "click on position " + arg2);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onUsingHeaderViewWidget();
    }

	private void onUsingHeaderViewWidget() {
		setContentView(R.layout.activity_main);
        
        GridView gv = (GridView) findViewById(R.id.gridView1);
        BaseAdapter adapter = new TestAdapter();
        TextView header = (TextView) getLayoutInflater().inflate(R.layout.griditem, gv, false);
        HeaderGridView hgv = (HeaderGridView)gv;
        hgv.addHeaderView(header, null, true);
        hgv.setAdapter(adapter);
        hgv.setOnItemClickListener(this);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static final String TAG = MainActivity.class.getName();
}
