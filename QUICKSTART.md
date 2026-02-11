# Quick Start Guide: meta-ollama-cpp

## 5-Minute Setup

### Prerequisites

```bash
# Ensure you have Yocto environment
cd yocto-playground
source sources/poky/oe-init-build-env
```

### 1. Add the Layer

```bash
# Add meta-ollama-cpp as a submodule (or clone it)
cd /path/to/yocto-playground
git submodule add https://github.com/Abhishekojha38/meta-ollama-cpp sources/meta-ollama-cpp

# Add to layers.conf
echo "sources/meta-ollama-cpp" >> layers.conf
```

### 2. Configure Build

Edit `build.conf` or `conf/local.conf`:

```bash
# Add packages to image
IMAGE_INSTALL:append = " llama-cpp ollama-cpp-server"

# Allocate space for models (10GB)
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"
```

### 3. Build

```bash
# Using CQFD (from yocto-playground)
cqfd init
cqfd run
```

### 4. Deploy & Test

```bash
# Run in QEMU
runqemu playground-ai-image nographic slirp


# On the device:
# 1. Download a model
wget -P /var/lib/ollama/models \
  https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf

# 2. Start the server
systemctl start ollama-cpp-server

# 3. Test
curl http://localhost:11434/api/generate -d '{
  "model": "tinyllama",
  "prompt": "Hello!"
}'
```

## Common Commands

```bash
# Check build dependencies
bitbake-layers show-recipes llama-cpp

# Clean rebuild
bitbake -c cleansstate llama-cpp
bitbake llama-cpp

# View logs during runtime
journalctl -u ollama-cpp-server -f
```

## Troubleshooting

**Build fails with CMake errors:**
```bash
# Check dependencies
bitbake -c cleanall llama-cpp
bitbake llama-cpp -c fetch -c unpack -c configure
```

**Server won't start:**
```bash
# Check service status
systemctl status ollama-cpp-server

# Check model permissions
ls -la /var/lib/ollama/models
chown -R ollama:ollama /var/lib/ollama
```

**Out of memory:**
```bash
# Reduce context size in wrapper
--ctx-size 1024  # instead of 2048

# Limit systemd service memory
# Edit ollama-cpp-server.service
MemoryLimit=2G
```

## Next Steps

- Read [INTEGRATION.md](INTEGRATION.md) for detailed integration
- See [COMPARISON.md](COMPARISON.md) for meta-ollama vs meta-ollama-cpp
- Check [README.md](README.md) for full documentation
- Browse recipes in `recipes-llm/` for customization

## Quick Reference

| Component | Location | Purpose |
|-----------|----------|---------|
| llama.cpp binary | `/usr/bin/llama-cli` | CLI inference |
| Server binary | `/usr/bin/llama-server` | HTTP server |
| Wrapper script | `/usr/bin/ollama-cpp-server` | Ollama-compatible |
| Models dir | `/var/lib/ollama/models/` | Model storage |
| Config | `/etc/ollama-cpp/config.json` | Server config |
| Service | `ollama-cpp-server.service` | Systemd unit |

Find models at: https://huggingface.co/models?library=gguf
