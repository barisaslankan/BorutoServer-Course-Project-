package com.barisaslankan.di

import com.barisaslankan.repository.HeroRepository
import com.barisaslankan.repository.HeroRepositoryImpl
import org.koin.dsl.module

val koinModule = module {
    single<HeroRepository>{
        HeroRepositoryImpl()
    }
}