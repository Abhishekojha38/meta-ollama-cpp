# Integration Guide: meta-ollama-cpp with yocto-playground

This guide explains how to integrate the `meta-ollama-cpp` layer into the Abhishekojha38/yocto-playground repository.

## Overview

The `meta-ollama-cpp` layer provides llama.cpp integration with an Ollama-compatible API for running large language models on embedded Linux systems built with Yocto.

## Integration Steps

### 1. Add Layer as Git Submodule

```bash
cd yocto-playground
git submodule add <your-meta-ollama-cpp-repo-url> sources/meta-ollama-cpp
git submodule update --init --recursive
```

### 2. Update layers.conf

Add to `yocto-playground/layers.conf`:

```
sources/meta-ollama-cpp
```

### 3. Update build.conf

Add to `yocto-playground/build.conf` if needed:

```bash
# Add llama-cpp to your image
IMAGE_INSTALL:append = " llama-cpp ollama-cpp-server"

# Increase rootfs space for AI models (10GB)
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"
```

### 4. Build the Image

Using the existing build system:

```bash
cqfd init
cqfd run
```

### 5. Testing in QEMU

```bash
# For x86-64
runqemu playground-x86 nographic slirp

# For ARM64
runqemu playground-arm64 nographic slirp
```

## Architecture Comparison

### meta-ollama vs meta-ollama-cpp

| Feature | meta-ollama | meta-ollama-cpp |
|---------|-------------|-----------------|
| Base | Ollama binary (Go) | llama.cpp (C++) |
| Size | Larger (~100MB+) | Smaller (~10MB) |
| Dependencies | Go runtime | Minimal (libc, libstdc++) |
| Performance | Good | Excellent on CPU |
| Memory | Higher | Lower |
| GPU Support | CUDA, ROCm | Limited (CPU-focused) |
| Best For | Desktop/Server | Embedded devices |

## Layer Structure

```
meta-ollama-cpp/
├── conf/
│   └── layer.conf              # Layer configuration
├── recipes-llm/
│   ├── llama-cpp/
│   │   └── llama-cpp_git.bb    # Core llama.cpp recipe
│   └── ollama-cpp-server/
│       ├── ollama-cpp-server_1.0.bb
│       └── files/
│           ├── ollama-cpp-server.service
│           ├── ollama-cpp-wrapper.sh
│           └── config.json
├── recipes-image/
│   └── images/
│       └── playground-ai-image.bb  # Example AI-enabled image
└── README.md
```

## Key Differences from meta-ollama

1. **Lighter Weight**: Uses llama.cpp instead of full Ollama (Go binary)
2. **Better for Embedded**: Optimized for resource-constrained devices
3. **Ollama Compatibility**: Provides Ollama-compatible API endpoint
4. **No Go Runtime**: Eliminates Go runtime dependency
5. **Direct C++ Implementation**: Better performance on ARM/embedded CPUs

## Configuration Options

### In local.conf or image recipe:

```bash
# Architecture optimizations
# ARM64 with NEON (automatic)
# x86-64 with AVX2 (automatic)

# Resource limits
# Adjust in systemd service file:
MemoryLimit=4G
CPUQuota=200%

# Model storage
OLLAMA_MODELS = "/var/lib/ollama/models"

# Server binding
OLLAMA_HOST = "0.0.0.0:11434"
```

## Runtime Usage

### Starting the Server

```bash
# Using systemd
systemctl start ollama-cpp-server
systemctl enable ollama-cpp-server

# Check status
systemctl status ollama-cpp-server

# View logs
journalctl -u ollama-cpp-server -f
```

### API Examples

```bash
# List models
curl http://localhost:11434/api/tags

# Generate text
curl -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama2:7b",
    "prompt": "Explain quantum computing",
    "stream": false
  }'

# Chat completion
curl -X POST http://localhost:11434/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama2:7b",
    "messages": [
      {"role": "user", "content": "Hello!"}
    ]
  }'
```

### Adding Models

```bash
# Download a GGUF model
cd /var/lib/ollama/models
wget https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.Q4_K_M.gguf

# Restart server to detect new model
systemctl restart ollama-cpp-server
```

## Troubleshooting

### Build Issues

```bash
# Clean rebuild
bitbake -c cleansstate llama-cpp
bitbake llama-cpp

# Check dependencies
bitbake-layers show-recipes llama-cpp
```

### Runtime Issues

```bash
# Check server logs
journalctl -u ollama-cpp-server -n 100

# Test llama.cpp directly
llama-cli -m /var/lib/ollama/models/model.gguf -p "test prompt"

# Check permissions
ls -la /var/lib/ollama/models
chown -R ollama:ollama /var/lib/ollama
```

## License

This layer follows the same licensing as llama.cpp (MIT License). Individual recipes may have different licenses - check the LIC_FILES_CHKSUM in each recipe.

## References

- [llama.cpp GitHub](https://github.com/ggerganov/llama.cpp)
- [Ollama API Documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)
- [Yocto Project Documentation](https://docs.yoctoproject.org/)
- [Original yocto-playground](https://github.com/Abhishekojha38/yocto-playground)
