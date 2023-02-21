package com.saulhdev.feeder.viewmodel

import com.saulhdev.feeder.models.Repository
import org.kodein.di.DI
import org.kodein.di.instance

class MainActivityViewModel(di: DI) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
}