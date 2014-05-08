package us.shandian.blacklight.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

public class WeiboSpan extends ClickableSpan
{
	private String mUrl;
	private Uri mUri;
	
	public WeiboSpan(String url) {
		mUrl = url;
		mUri = Uri.parse(mUrl);
	}
	
	public String getURL() {
		return mUrl;
	}
	
	@Override
	public void onClick(View v) {
		Context context = v.getContext();
		
		if (mUri.getScheme().startsWith("http")) {
			// TODO View some weibo pages inside app
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(mUri);
			context.startActivity(i);
		} else {
			// TODO Jump to other activities in this app
		}
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(ds.linkColor);
		ds.setUnderlineText(false);
	}

}
