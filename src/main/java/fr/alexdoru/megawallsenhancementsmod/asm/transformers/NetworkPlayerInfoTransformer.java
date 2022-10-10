package fr.alexdoru.megawallsenhancementsmod.asm.transformers;

import fr.alexdoru.megawallsenhancementsmod.asm.ASMLoadingPlugin;
import fr.alexdoru.megawallsenhancementsmod.asm.IMyClassTransformer;
import fr.alexdoru.megawallsenhancementsmod.asm.InjectionStatus;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class NetworkPlayerInfoTransformer implements IMyClassTransformer {

    @Override
    public String getTargetClassName() {
        return "net.minecraft.client.network.NetworkPlayerInfo";
    }

    @Override
    public ClassNode transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(2);
        addInterface(classNode, "NetworkPlayerInfoAccessor");
        classNode.visitField(ACC_PUBLIC, "playerFinalkills", "I", null, 0).visitEnd();
        addSetterMethod(
                classNode,
                "setPlayerFinalkills",
                ASMLoadingPlugin.isObf ? "bdc" : "net/minecraft/client/network/NetworkPlayerInfo",
                "playerFinalkills",
                "I",
                null);
        for (final MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>") && methodNode.desc.equals(ASMLoadingPlugin.isObf ? "(Lgz$b;)V" : "(Lnet/minecraft/network/play/server/S38PacketPlayerListItem$AddPlayerData;)V")) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (insnNode.getOpcode() == PUTFIELD && ((FieldInsnNode) insnNode).owner.equals(ASMLoadingPlugin.isObf ? "bdc" : "net/minecraft/client/network/NetworkPlayerInfo") && ((FieldInsnNode) insnNode).name.equals(ASMLoadingPlugin.isObf ? "h" : "displayName")) {
                        /*
                         * Replace line 48 with :
                         * this.displayname = NetworkPlayerInfoHook.getDisplayName(this.displayname, this.gameProfile)
                         */
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new FieldInsnNode(GETFIELD, ASMLoadingPlugin.isObf ? "bdc" : "net/minecraft/client/network/NetworkPlayerInfo", ASMLoadingPlugin.isObf ? "a" : "gameProfile", "Lcom/mojang/authlib/GameProfile;"));
                        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("NetworkPlayerInfoHook"), "getDisplayName", ASMLoadingPlugin.isObf ? "(Leu;Lcom/mojang/authlib/GameProfile;)Leu;" : "(Lnet/minecraft/util/IChatComponent;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/util/IChatComponent;", false));
                        methodNode.instructions.insertBefore(insnNode, list);
                        status.addInjection();

                        /*Adds after line 48 : this.playersFinalKills = NetworkPlayerInfoHook.getPlayersFinals(this.gameProfile.getName())*/
                        final AbstractInsnNode nextNode = insnNode.getNext();
                        if (nextNode != null) {
                            final InsnList list2 = new InsnList();
                            list2.add(new VarInsnNode(ALOAD, 0));
                            list2.add(new VarInsnNode(ALOAD, 0));
                            list2.add(new FieldInsnNode(GETFIELD, ASMLoadingPlugin.isObf ? "bdc" : "net/minecraft/client/network/NetworkPlayerInfo", ASMLoadingPlugin.isObf ? "a" : "gameProfile", "Lcom/mojang/authlib/GameProfile;"));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getName", "()Ljava/lang/String;", false));
                            list2.add(new MethodInsnNode(INVOKESTATIC, getHookClass("NetworkPlayerInfoHook"), "getPlayersFinals", "(Ljava/lang/String;)I", false));
                            list2.add(new FieldInsnNode(PUTFIELD, ASMLoadingPlugin.isObf ? "bdc" : "net/minecraft/client/network/NetworkPlayerInfo", "playerFinalkills", "I"));
                            methodNode.instructions.insertBefore(nextNode, list2);
                            status.addInjection();
                        }

                        return classNode;
                    }
                }
            }
        }
        return classNode;
    }

}
