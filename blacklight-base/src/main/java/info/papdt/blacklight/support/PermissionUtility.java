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

package info.papdt.blacklight.support;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import info.papdt.blacklight.R;

public class PermissionUtility {
	public static void storage(Context context, final Runnable granted) {
		PermissionListener grantedListener = new PermissionListener() {
			@Override
			public void onPermissionGranted(PermissionGrantedResponse response) {
				granted.run();
			}

			@Override
			public void onPermissionDenied(PermissionDeniedResponse response) {
			}

			@Override
			public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
			}
		};

		PermissionListener dialogListener = DialogOnDeniedPermissionListener.Builder
				.withContext(context)
				.withTitle(R.string.perm_sdcard_title)
				.withMessage(R.string.perm_sdcard)
				.withButtonText(android.R.string.ok)
				.withIcon(R.drawable.ic_launcher)
				.build();

		Dexter.checkPermission(new CompositePermissionListener(grantedListener, dialogListener), Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	public static boolean hasStoragePermission(Context context) {
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ||
			(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
	}
}
