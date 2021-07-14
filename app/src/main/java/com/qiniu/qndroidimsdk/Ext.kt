package com.qiniu.qndroidimsdk

import android.widget.Toast
import com.qiniu.qndroidimsdk.util.AppCache


fun String.asToast(){
        Toast.makeText(AppCache.getContext(),this, Toast.LENGTH_SHORT).show()
    }