/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.viewmodel

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.SourceRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module

open class KoinComponentActivity : AppCompatActivity(), KoinComponent {
    private val modelModule = module {
        viewModelOf(::EditFeedViewModel)
        viewModelOf(::SearchFeedViewModel)
        viewModelOf(::SourcesViewModel)
    }

    private val repositoryModule = module {
        single { ArticleRepository(this@KoinComponentActivity) }
        single { SourceRepository(this@KoinComponentActivity) }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        startKoin {
            androidLogger()
            androidContext(this@KoinComponentActivity)
            modules(repositoryModule)
            modules(modelModule)
        }
    }

    override fun onStop() {
        super.onStop()
        stopKoin()
    }
}