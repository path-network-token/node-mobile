package network.path.mobilenode.di

import network.path.mobilenode.BuildConfig
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.ui.intro.DisclaimerViewModel
import network.path.mobilenode.ui.intro.IntroViewModel
import network.path.mobilenode.ui.main.MainViewModel
import network.path.mobilenode.ui.main.dashboard.DashboardViewModel
import network.path.mobilenode.ui.main.jobreport.JobReportViewModel
import network.path.mobilenode.ui.opengl.glutils.ObjLoader
import network.path.mobilenode.ui.opengl.models.providers.ObjDataProvider
import network.path.mobilenode.ui.opengl.models.providers.SphereDataProvider
import network.path.mobilenode.ui.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {
    single { ObjDataProvider(ObjLoader(androidApplication(), "models/ico.obj", radius = 1f)) }
    single { SphereDataProvider(2, 1.1f) }

    single { PathSystem.create(androidApplication(), BuildConfig.DEBUG) }

    viewModel { IntroViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { JobReportViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { DisclaimerViewModel() }
    viewModel { MainViewModel(get()) }
}

