'use strict';

const gulp = require('gulp'),
    yargs = require('yargs'),
    del = require('del'),
    debug = require('gulp-debug'),
    $ = require('gulp-load-plugins')(),
    browserify = require('browserify'),
    babelify = require('babelify'),
    watchify = require('watchify'),
    source = require('vinyl-source-stream'),
    env = require('gulp-env'),
    rename = require('gulp-rename'),
    gulpif = require('gulp-if'),
    preprocess = require('gulp-preprocess'),
    preprocessify = require('preprocessify'),

    appName = yargs.argv.app || 'control',
    srcAppScriptFile = 'app/scripts/' + appName + 'App.js',
    srcAppCssFile = 'app/styles/main.css',

    distFolders = ['dist/' + appName + '/**', 'dist-rev/' + appName + '/**', 'dist-min/' + appName + '/**'],
    distScriptFile = 'app.js',
    distScriptFolder = 'dist/' + appName + '/scripts',
    distCssFolder = 'dist/' + appName + '/styles',

    envVars = {
        ENV: 'prod',
        BABEL_ENV: 'prod',
        NODE_ENV: 'production', //cause using production version of react
        BUILD_DATE: new Date().toUTCString()
    };

$.util.log('Building app ', appName);

gulp.task('env-dev', function () {
    envVars.ENV = 'dev';
    envVars.BABEL_ENV = 'dev';

    env({vars: envVars});
    $.util.log('Using config ', envVars)
});

gulp.task('env-prod', function () {
    //nothing

    env({vars: envVars});
    $.util.log('Using config ', envVars)
});

/**
 * Cleans dist and dist-rev
 */
gulp.task('clean', function (cb) {
    $.cache.clearAll();
    cb(del.sync(distFolders));
});

/**
 * Bundles app script to single file
 */
gulp.task('appScript', function () {
    return createBuildBundler().bundle();
});

/**
 * Watches for app scripts changes.
 */
gulp.task('watchAppScript', function () {
    return createWatchBundler().bundle();
});

/**
 * Moves app style to dist
 */
gulp.task('appStyle', function () {
    return gulp.src(srcAppCssFile)
        .pipe(gulp.dest(distCssFolder));
});

/**
 * Moves index.html to dist
 */
gulp.task('html', function () {
    return gulp.src('app/' + appName + '.html')
        .pipe($.useref())
        .pipe(gulpif('*.html', rename('index.html')))
        .pipe(preprocess({context: envVars}))
        .pipe(gulp.dest('dist/' + appName));
});

/**
 * Moves vendor fonts to dist
 */
gulp.task('fonts', function () {
    return gulp.src(['app/bower_components/bootstrap/dist/fonts/*.{eot,svg,ttf,woff,woff2}'])
        .pipe(gulp.dest('dist/' + appName + '/fonts'));
});

/**
 * Moves images to dist
 */
gulp.task('images', function () {
    return gulp.src(['app/images/**'])
        .pipe(gulp.dest('dist/' + appName + '/images'));
});

/**
 * Moves extras files like favicon and robots to dist
 */
gulp.task('extras', function () {
    return gulp.src(['app/*.txt', 'app/*.ico', 'app/*icon*.png'])
        .pipe(gulp.dest('dist/' + appName))
        .pipe($.size());
});

/**
 * Watches for resource changes.
 */
gulp.task('watch', ['env-dev', 'clean', 'appStyle', 'watchAppScript', 'html', 'fonts', 'images', 'extras'], function () {
    // Watch .html files
    gulp.watch('app/' + appName + '.html', ['html']);
    // Watch app styles
    gulp.watch(['app/styles/**/*.css'], ['appStyle']);
    // Watch image files
    gulp.watch('app/images/**/*', ['images']);
});

/**
 * Collect all resources and move to dist
 */
gulp.task('dist', ['env-prod', 'clean', 'appScript', 'appStyle', 'html', 'fonts', 'images', 'extras'], function () {
    return gulp.src('dist/' + appName + '/scripts/*.js')
        .pipe(debug({title: 'Task dist'}))
        .pipe($.uglify())
        .pipe($.stripDebug())
        .pipe(gulp.dest('dist-min/' + appName + '/scripts'));
});

/**
 * Calculate hash for scripts and styles, append to filenames and move resulting files to dist-rev
 */
gulp.task('revision', ['dist'], function () {
    return gulp.src(['dist/' + appName + '/**/*.css', 'dist-min/' + appName + '/**/*.js'])
        .pipe($.rev())
        .pipe(gulp.dest('dist-rev/' + appName))
        .pipe($.rev.manifest())
        .pipe(gulp.dest('dist/' + appName))
});

/**
 * Move extras to dist-rev
 */
gulp.task('moveExtrasToRev', ['dist'], function () {
    return gulp.src(['dist/' + appName + '/**/!(*.css|*.js)'])
        .pipe(gulp.dest('dist-rev/' + appName))
});

/**
 * Change references to scripts and styles in index.html and move resulting file to dist-rev
 */
gulp.task('revreplace', ['revision'], function () {
    const manifest = gulp.src('dist/' + appName + '/rev-manifest.json');

    return gulp.src('dist/' + appName + '/index.html')
        .pipe($.revReplace({manifest: manifest}))
        .pipe(gulp.dest('dist-rev/' + appName));
});

/**
 * Default task. Builds ready to upload app package.
 */
gulp.task('default', ['env-prod', 'clean', 'revreplace', 'moveExtrasToRev']);

function createWatchBundler() {
    const bundler = browserify({
        entries: [srcAppScriptFile],
        debug: true,
        insertGlobals: true,
        cache: {},
        packageCache: {},
        fullPaths: true,
        plugin: [watchify]
    });

    bundler.on('update', doBundle);
    bundler.on('log', $.util.log);

    $.util.log('Bundler created');

    function doBundle() {
        $.util.log('Bundling started');
        return bundler
            .transform(preprocessify, {context: envVars})
            .transform(babelify)
            .bundle()
            // log errors if they happen
            .on('error', $.util.log.bind($.util, 'Bundling error'))
            .pipe(source(distScriptFile))
            .pipe(gulp.dest(distScriptFolder))
            .on('end', function () {
                $.util.log('Bundling finished');
            });
    }

    return {bundle: doBundle}
}

function createBuildBundler() {
    const bundler = browserify({
        entries: [srcAppScriptFile],
        debug: true,
        insertGlobals: true,
        fullPaths: true
    });

    bundler.on('log', $.util.log);

    $.util.log('Bundler created');

    function doBundle() {
        $.util.log('Bundling started');
        return bundler
            .transform(preprocessify, {context: envVars})
            .transform(babelify)
            .bundle()
            // log errors if they happen
            .on('error', $.util.log.bind($.util, 'Bundling error'))
            .pipe(source(distScriptFile))
            .pipe(gulp.dest(distScriptFolder))
            .on('end', function () {
                $.util.log('Bundling finished');
            });
    }

    return {bundle: doBundle}
}