#!/bin/bash

# please set your cargo rustc python location
# more see https://github.com/mozilla/rust-android-gradle#specifying-paths-to-sub-commands-python-cargo-and-rustc
export RUST_ANDROID_GRADLE_PYTHON_COMMAND=$HOME/.pyenv/shims/python # set the path on your computer
export RUST_ANDROID_GRADLE_CARGO_COMMAND=$HOME/.cargo/bin/cargo # set the path on your computer
export RUST_ANDROID_GRADLE_RUSTC_COMMAND=$HOME/.cargo/bin/rustc # set the path on your computer