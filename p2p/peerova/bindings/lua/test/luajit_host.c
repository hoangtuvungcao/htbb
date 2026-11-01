/* Minimal LuaJIT host: runs a Lua file under libluajit-5.1 when no `luajit` executable is installed.
 * Prototypes declared by hand (Lua 5.1 API) so no LuaJIT headers are needed.
 *   cc -o luajit_host luajit_host.c -l:libluajit-5.1.so.2     (or -lluajit-5.1)
 *   ./luajit_host script.lua                                   */
#include <stdio.h>
#include <stddef.h>
typedef struct lua_State lua_State;
lua_State* luaL_newstate(void);
void luaL_openlibs(lua_State* L);
int luaL_loadfile(lua_State* L, const char* filename);
int lua_pcall(lua_State* L, int nargs, int nresults, int errfunc);
const char* lua_tolstring(lua_State* L, int idx, size_t* len);
void lua_pushstring(lua_State* L, const char* s);
void lua_createtable(lua_State* L, int narr, int nrec);
void lua_rawseti(lua_State* L, int idx, int n);
void lua_setfield(lua_State* L, int idx, const char* k);
void lua_close(lua_State* L);
#define LUA_GLOBALSINDEX (-10002)

int main(int argc, char** argv) {
    if (argc < 2) { fprintf(stderr, "usage: %s script.lua\n", argv[0]); return 2; }
    lua_State* L = luaL_newstate();
    luaL_openlibs(L);
    lua_createtable(L, argc, 0); /* global `arg`, arg[0] = script */
    for (int i = 1; i < argc; i++) { lua_pushstring(L, argv[i]); lua_rawseti(L, -2, i - 1); }
    lua_setfield(L, LUA_GLOBALSINDEX, "arg");
    if (luaL_loadfile(L, argv[1]) || lua_pcall(L, 0, 0, 0)) {
        fprintf(stderr, "error: %s\n", lua_tolstring(L, -1, NULL));
        lua_close(L);
        return 1;
    }
    lua_close(L);
    return 0;
}
