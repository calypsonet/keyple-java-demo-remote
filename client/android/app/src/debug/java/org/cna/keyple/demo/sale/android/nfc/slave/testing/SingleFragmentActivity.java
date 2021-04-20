/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 ********************************************************************************/
package org.cna.keyple.demo.sale.android.nfc.slave.testing;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import org.eclipse.keyple.demo.remote.R;

public class SingleFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        layout.setId(R.id.container);
        setContentView(layout);
    }

    public void setFragment(Fragment fragment) {
        this.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, "TEST")
                .commit();
    }

    public void replaceFragment(Fragment fragment) {
        this.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                .commit();
    }
}
