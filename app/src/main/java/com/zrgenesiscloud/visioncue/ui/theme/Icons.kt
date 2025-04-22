package com.zrgenesiscloud.visioncue.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.zrgenesiscloud.visioncue.android.R
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom icons for the teleprompter application.
 * Provides app-specific icons and extends the standard Material Icons.
 */
object CustomIcons {
    /**
     * App-specific icons loaded from drawable resources
     */
    val AppLogo: ImageVector
        @Composable
        get() {
            if (_appLogo != null) {
                return _appLogo!!
            }
            _appLogo = materialIcon(name = "AppLogo") {
                materialPath {
                    // This path data comes from your app_logo.xml
                    moveTo(12f, 2f)
                    lineTo(2f, 7f)
                    verticalLineTo(17f)
                    lineTo(12f, 22f)
                    lineTo(22f, 17f)
                    verticalLineTo(7f)
                    lineTo(12f, 2f)
                    
                    // Draw diamond shape
                    moveTo(12f, 4.236f)
                    lineTo(18.88f, 7.676f)
                    lineTo(12f, 11.116f)
                    lineTo(5.12f, 7.676f)
                    lineTo(12f, 4.236f)
                    
                    // Left part
                    moveTo(4f, 8.884f)
                    lineTo(11f, 12.324f)
                    verticalLineTo(19.116f)
                    lineTo(4f, 15.676f)
                    verticalLineTo(8.884f)
                    
                    // Right part
                    moveTo(13f, 19.116f)
                    verticalLineTo(12.324f)
                    lineTo(20f, 8.884f)
                    verticalLineTo(15.676f)
                    lineTo(13f, 19.116f)
                }
            }
            return _appLogo!!
        }
    private var _appLogo: ImageVector? = null
    
    /**
     * Upload icon with consistent sizing as material icons
     */
    val Upload: ImageVector
        @Composable
        get() {
            if (_upload != null) {
                return _upload!!
            }
            _upload = ImageVector.vectorResource(id = R.drawable.ic_upload)
            return _upload!!
        }
    private var _upload: ImageVector? = null

    
    val File: ImageVector
    @Composable
    get() {
        if (_file != null) {
            return _file!!
        }
        _file = ImageVector.vectorResource(id = R.drawable.ic_file)
        return _file!!
    }
    private var _file: ImageVector? = null
    /**
     * Custom icon sets grouped by category
     */
    object Teleprompter {
        /**
         * Icons related to teleprompter controls
         */
        val Start = Icons.Filled.PlayArrow
        
        // You can add more custom teleprompter-specific icons here
        // For example:
        // val Speed: ImageVector
        //     @Composable
        //     get() = ImageVector.vectorResource(id = R.drawable.ic_speed)
    }
    
    /**
     * Icons for different status indicators
     */
    object Status {
        val Success = Icons.Rounded.CheckCircle
        val Warning = Icons.Rounded.Warning
        val Error = Icons.Rounded.Error
        val Info = Icons.Rounded.Info
    }
}

/**
 * Extension functions for Icons to access custom icons
 */
val Icons.Custom: CustomIcons
    get() = CustomIcons 