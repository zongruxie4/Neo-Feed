/*
 * This file is part of Omega Feeder
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.ofrss.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.saulhdev.ofrss.R

@Composable
fun FeedItem(
    feedTitle:String ="",
    feedURL:String ="",
    feedImage:String = "",
    description:String =""
){
    Column(
        modifier = Modifier
            .padding(
            16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if(feedImage.isNotEmpty()){
                Image(
                    painter = rememberAsyncImagePainter(feedImage.toUri()),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            else{
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = feedTitle)
                Text(text = feedURL)
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = { /*TODO*/ }
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(text = description)
    }
}

@Preview
@Composable
fun FeedItemPreview(){
    FeedItem(
        feedTitle = "Feed Title",
        feedURL = "https://www.feedurl.com",
        description = "Feed Description"
    )
}