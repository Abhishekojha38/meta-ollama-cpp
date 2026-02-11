SUMMARY = "Port of Facebook's LLaMA model in C/C++"
DESCRIPTION = "llama.cpp is a plain C/C++ implementation of Meta's LLaMA model with minimal dependencies for efficient local LLM inference"
HOMEPAGE = "https://github.com/ggerganov/llama.cpp"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b5330f450b52b1d8b7f4c82b1f1e5e74"

SRCREV = "ec91f21dd3db7e075eade22f28ca0b63bc01ad2c"
SRC_URI = "git://github.com/ggerganov/llama.cpp.git;protocol=https;branch=master"

S = "${WORKDIR}/git"

DEPENDS = "curl"

inherit cmake pkgconfig

# Architecture-specific optimizations
EXTRA_OECMAKE = ""

# Enable Metal backend for Apple Silicon (not applicable for most embedded)
# EXTRA_OECMAKE += "-DLLAMA_METAL=ON"

# For ARM64 with NEON support
EXTRA_OECMAKE:append:aarch64 = " -DLLAMA_NATIVE=OFF -DLLAMA_BLAS=OFF"

# For x86_64 with AVX2 support
EXTRA_OECMAKE:append:x86-64 = " -DLLAMA_AVX2=ON -DLLAMA_FMA=ON"

# Disable CUDA/ROCm for embedded systems
EXTRA_OECMAKE += " -DLLAMA_CUBLAS=OFF -DLLAMA_HIPBLAS=OFF"

# Enable server build
EXTRA_OECMAKE += " -DLLAMA_SERVER_VERBOSE=ON -DBUILD_SHARED_LIBS=ON"

# Disable some optional features to reduce dependencies
EXTRA_OECMAKE += " -DLLAMA_CLBLAST=OFF"

do_install:append() {
    # Install binaries
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/main ${D}${bindir}/llama-cli
    install -m 0755 ${B}/bin/server ${D}${bindir}/llama-server
    install -m 0755 ${B}/bin/quantize ${D}${bindir}/llama-quantize
    install -m 0755 ${B}/bin/perplexity ${D}${bindir}/llama-perplexity
    
    # Install shared libraries
    install -d ${D}${libdir}
    install -m 0755 ${B}/libllama.so ${D}${libdir}/libllama.so
    
    # Install headers for development
    install -d ${D}${includedir}/llama
    install -m 0644 ${S}/llama.h ${D}${includedir}/llama/
    install -m 0644 ${S}/ggml.h ${D}${includedir}/llama/
}

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} = "${bindir}/* ${libdir}/*.so"
FILES:${PN}-dev = "${includedir}/*"
FILES:${PN}-dbg = "${bindir}/.debug ${libdir}/.debug"

# Allow already-stripped binaries (llama.cpp pre-strips some)
INSANE_SKIP:${PN} += "already-stripped ldflags"

RDEPENDS:${PN} = "libcurl"
