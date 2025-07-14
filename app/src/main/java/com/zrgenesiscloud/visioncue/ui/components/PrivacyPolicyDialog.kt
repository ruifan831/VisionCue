package com.zrgenesiscloud.visioncue.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.system.exitProcess

@Composable
fun PrivacyPolicyDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = { /* Do nothing, prevent dismissal */ }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "隐私政策",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            《提词器大师》应用隐私政策

                            最后更新日期：2025年07月15日

                            我们重视您的隐私。本隐私政策描述了我们如何收集、使用、存储和保护您的个人信息。

                            1. 信息收集与使用
                            
                            我们收集以下信息用于广告展示和应用改进：
                            • 设备信息：设备型号、操作系统版本
                            • 应用使用情况：功能使用频率、崩溃数据
                            • 网络信息：IP地址、网络类型
                            
                            2. 广告
                            
                            本应用使用第三方广告服务（穿山甲SDK）来显示广告。广告服务可能会收集和使用数据来提供个性化广告。您可以在应用设置中选择限制个性化广告。
                            
                            3. 数据存储
                            
                            您创建的脚本内容仅存储在您的设备本地，不会上传至我们的服务器。
                            
                            4. 权限说明
                            
                            本应用需要以下权限：
                            • 网络访问：用于广告加载
                            • 存储权限：用于保存脚本内容
                            
                            5. 隐私选择
                            
                            您可以随时在应用设置中管理广告个性化选项。
                            
                            6. 政策更新
                            
                            我们可能会不时更新本隐私政策。更新后，我们会在应用内通知您重要变更。
                            
                            7. 联系我们
                            
                            如有任何疑问或建议，请联系：support@zrgenesiscloud.com
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDecline,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("不同意并退出")
                    }
                    
                    Button(
                        onClick = onAccept
                    ) {
                        Text("同意并继续")
                    }
                }
            }
        }
    }
} 