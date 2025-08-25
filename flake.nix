{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    systems.url = "github:nix-systems/default";
  };

  outputs =
    inputs:
    inputs.flake-parts.lib.mkFlake { inherit inputs; } {
      systems = import inputs.systems;
      perSystem =
        {
          config,
          self',
          pkgs,
          lib,
          system,
          ...
        }:
        let
          libs = with pkgs; [
            libGL
            glfw
            openal
            flite
            jdk
          ];
        in
        {
          devShells.default = pkgs.mkShell {
            packages = with pkgs; [
              jetbrains.idea-community
              jdt-language-server
            ];
            buildInputs = libs;
            LD_LIBRARY_PATH = lib.makeLibraryPath libs;
          };
        };
    };
}
