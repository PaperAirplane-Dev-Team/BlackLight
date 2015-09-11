/*
 * Copyright (C) 2015 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.papdt.blacklight.ui.statuses;

import android.os.Bundle;

import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.ui.common.AbsImageActivity;

public class StatusImageActivity extends AbsImageActivity<HomeTimeLineApiCache> {
	private MessageModel mModel;

	@Override
	protected HomeTimeLineApiCache buildApiCache() {
		return new HomeTimeLineApiCache(this);
	}

	@Override
	protected Object[] doDownload(Object[] params) {
		int id = Integer.parseInt(params[1].toString());
		Object img = getApiCache().getLargePic(mModel, id, (MyCallback) params[2]);
		mLoaded[id] = true;
		return new Object[]{params[0], img};
	}

	@Override
	protected String saveLargePic(int current) {
		return getApiCache().saveLargePic(mModel, current);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mModel = getIntent().getParcelableExtra("model");
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getCount() {
		return mModel.hasMultiplePictures() ? mModel.pic_urls.size() : 1;
	}
}
